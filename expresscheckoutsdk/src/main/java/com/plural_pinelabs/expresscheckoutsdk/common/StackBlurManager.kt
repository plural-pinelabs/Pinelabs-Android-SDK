package com.plural_pinelabs.expresscheckoutsdk.common

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import kotlin.math.max
import kotlin.math.min

/**
 * A simplified StackBlur implementation for Bitmaps.
 * This should be run on a background thread.
 *
 * For a real production app, consider using a highly optimized native blur library
 * or RenderScript (if compatible with your min API level) for better performance.
 */
object StackBlurManager {

    @WorkerThread
    fun process(originalBitmap: Bitmap, radius: Float): Bitmap {
        if (radius <= 0) return originalBitmap

        // Coerce radius to a reasonable integer range.
        // A higher radius results in more blur but also more processing time.
        val r = radius.toInt().coerceIn(1, 25) // Max radius 25 is typical for performance

        val width = originalBitmap.width
        val height = originalBitmap.height

        // Create a mutable copy of the original bitmap to apply blur on
        val blurredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(width * height)
        // Get all pixels from the bitmap into the array
        blurredBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val wm = width - 1  // max valid column index
        val hm = height - 1 // max valid row index

        // Stack for blur values (Red, Green, Blue components)
        // Its size is 2*radius + 1
        val stack = Array(r + r + 1) { IntArray(3) }

        var p: Int // Pixel value (ARGB)
        var sumR: Int
        var sumG: Int
        var sumB: Int
        var mulSum: Int // Multiplier sum (number of pixels in the current stack)
        var yi: Int // Y index (current row's starting pixel index)
        var yp: Int // Y pixel position (current column's starting pixel index)

        // --- Horizontal blur pass ---
        yi = 0 // Initialize to the start of the first row
        for (h in 0 until height) { // Iterate through each row
            sumR = 0
            sumG = 0
            sumB = 0
            mulSum = 0

            // Initialize the stack for the beginning of the row
            // The stack will contain pixels from column (current_col - r) to (current_col + r)
            // For the first pixel of the row, `current_col` is 0.
            // So we sample pixels from `max(0, 0-r)` to `min(wm, 0+r)`.
            // We use the `stackpointer` to fill the stack and ensure correct "wrapping" if needed later.
            var stackPointer = r // Start filling from the middle of the stack
            for (j in -r..r) {
                // Calculate the source column index relative to the beginning of the row (column 0)
                // This `srcCol` will be clamped between 0 and wm.
                val srcCol = min(wm, max(0, j)) // Clamp column index to [0, wm]
                val currentPixelIndex = yi + srcCol // Absolute index in pixels array

                p = pixels[currentPixelIndex]

                val sir = stack[stackPointer % stack.size] // Use modulo for circular buffer
                sir[0] = (p shr 16) and 0xFF // Red
                sir[1] = (p shr 8) and 0xFF  // Green
                sir[2] = p and 0xFF          // Blue

                sumR += sir[0]
                sumG += sir[1]
                sumB += sir[2]
                mulSum++
                stackPointer++
            }

            stackPointer = r // Reset to the middle for processing
            var stackstart = 0

            for (w in 0 until width) { // Iterate through each pixel in the current row
                pixels[yi] = (0xFF shl 24) or // Alpha channel (full opaque)
                        ((sumR / mulSum) shl 16) or // Red component
                        ((sumG / mulSum) shl 8) or  // Green component
                        (sumB / mulSum)             // Blue component

                // Remove the pixel at `stackstart` from the sums
                val removedSir = stack[stackstart]
                sumR -= removedSir[0]
                sumG -= removedSir[1]
                sumB -= removedSir[2]

                // Advance `stackstart` for the next iteration
                stackstart++
                if (stackstart == stack.size) { // Check if stackstart wraps around
                    stackstart = 0
                }

                // Advance `stackpointer` for the next pixel to add
                stackPointer++
                if (stackPointer == stack.size) { // Check if stackpointer wraps around
                    stackPointer = 0
                }

                // Calculate the column index for the pixel to add.
                // It's 'r' pixels ahead of the current pixel 'w', clamped to 'wm'.
                val addPixelCol = min(wm, w + r + 1) // Clamp the column to be added
                val addPixelIndex = yi + addPixelCol // Absolute index in pixels array

                // This is the line that was throwing the error.
                // We're now much more careful about `addPixelIndex`'s calculation.
                p = pixels[addPixelIndex]
                val addedSir = stack[stackPointer] // Use the correctly advanced stackPointer
                addedSir[0] = (p shr 16) and 0xFF
                addedSir[1] = (p shr 8) and 0xFF
                addedSir[2] = p and 0xFF

                sumR += addedSir[0]
                sumG += addedSir[1]
                sumB += addedSir[2]

                yi++ // Move to the next pixel index in the row
            }
        }

        // --- Vertical blur pass ---
        // We reuse the `pixels` array, which now contains horizontally blurred data.
        for (w in 0 until width) { // Iterate through each column
            sumR = 0
            sumG = 0
            sumB = 0
            mulSum = 0

            // Initialize the stack for the beginning of the column
            // We sample pixels from `max(0, 0-r)` to `min(hm, 0+r)` rows for the first column pixel.
            var stackPointer = r
            for (j in -r..r) {
                // Calculate the source row index relative to the beginning of the column (row 0)
                // This `srcRow` will be clamped between 0 and hm.
                val srcRow = min(hm, max(0, j)) // Clamp row index to [0, hm]
                val currentPixelIndex = w + srcRow * width // Absolute index

                p = pixels[currentPixelIndex]

                val sir = stack[stackPointer % stack.size] // Use modulo for circular buffer
                sir[0] = (p shr 16) and 0xFF
                sir[1] = (p shr 8) and 0xFF
                sir[2] = p and 0xFF

                sumR += sir[0]
                sumG += sir[1]
                sumB += sir[2]
                mulSum++
                stackPointer++
            }

            stackPointer = r
            var stackstart = 0
            yp = w // Starting absolute index for the current column (first pixel of the column)

            for (h in 0 until height) { // Iterate through each pixel in the current column
                pixels[yp] = (0xFF shl 24) or
                        ((sumR / mulSum) shl 16) or
                        ((sumG / mulSum) shl 8) or
                        (sumB / mulSum)

                // Remove the pixel at `stackstart` from the sums
                val removedSir = stack[stackstart]
                sumR -= removedSir[0]
                sumG -= removedSir[1]
                sumB -= removedSir[2]

                // Advance `stackstart` for the next iteration
                stackstart++
                if (stackstart == stack.size) {
                    stackstart = 0
                }

                // Advance `stackpointer` for the next pixel to add
                stackPointer++
                if (stackPointer == stack.size) {
                    stackPointer = 0
                }

                // Add the new pixel to the stack
                // Calculate the row index for the pixel to add.
                // It's 'r' pixels ahead of the current pixel 'h', clamped to 'hm'.
                val addPixelRow = min(hm, h + r + 1) // Clamp the row to be added
                val addPixelIndex = w + addPixelRow * width // Absolute index

                // This was also a potential source of error.
                p = pixels[addPixelIndex]
                val addedSir = stack[stackPointer] // Use the correctly advanced stackPointer
                addedSir[0] = (p shr 16) and 0xFF
                addedSir[1] = (p shr 8) and 0xFF
                addedSir[2] = p and 0xFF

                sumR += addedSir[0]
                sumG += addedSir[1]
                sumB += addedSir[2]

                yp += width // Move to the next pixel index in the column
            }
        }

        // Set the processed pixels back to the bitmap
        blurredBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return blurredBitmap
    }
}