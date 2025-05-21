package com.plural_pinelabs.expresscheckoutsdk.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import com.plural_pinelabs.expresscheckoutsdk.R

class OtpInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val otpEditTexts: MutableList<EditText> = mutableListOf()
    private var otpDigits: Int = 6
    private var boxSpacing: Int = 0
    private var boxBackgroundDrawable: Drawable? = null

    // Properties for child EditText styling
    private var otpTextColor: Int = Color.BLACK
    private var otpTextSize: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics
    )
    private var otpTextStyle: Int = Typeface.NORMAL
    private var otpCursorVisible: Boolean = false

    init {
        context.withStyledAttributes(attrs, R.styleable.OtpInputView, defStyleAttr, 0) {
            otpDigits = getInt(R.styleable.OtpInputView_otpDigits, 6)
            boxSpacing = getDimensionPixelSize(R.styleable.OtpInputView_boxSpacing, 0)
            boxBackgroundDrawable = context.getDrawable(R.drawable.otp_box_background)

            // Read custom text and EditText properties
            otpTextColor = getColor(R.styleable.OtpInputView_otpTextColor, Color.BLACK)
            otpTextSize = getDimension(R.styleable.OtpInputView_otpTextSize, otpTextSize)
            otpTextStyle = getInt(R.styleable.OtpInputView_otpTextStyle, Typeface.NORMAL)
            otpCursorVisible = getBoolean(R.styleable.OtpInputView_otpCursorVisible, false)

            // Note: We are not directly reading boxCornerRadius, boxStrokeColor, etc. here
            // because the otp_box_background.xml selector handles those based on state.
            // If you wanted to set these programmatically or via attributes for the shape drawable,
            // you would need to parse the drawable or create shapes programmatically.
            // Using the state list drawable is simpler for state changes (focused/default).


        }

        setupEditTexts()
    }

    private fun setupEditTexts() {
        for (i in 0 until otpDigits) {
            val editText = EditText(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT, // Width will be calculated in onMeasure
                    LayoutParams.WRAP_CONTENT // Height will be calculated in onMeasure
                )
                gravity = Gravity.CENTER
                filters = arrayOf(InputFilter.LengthFilter(1))

                // Apply properties read from custom attributes
                inputType = InputType.TYPE_CLASS_NUMBER
                isCursorVisible = this@OtpInputView.otpCursorVisible
                setTextColor(this@OtpInputView.otpTextColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, this@OtpInputView.otpTextSize)

                // Apply text style
                typeface = when (this@OtpInputView.otpTextStyle) {
                    Typeface.BOLD -> Typeface.defaultFromStyle(Typeface.BOLD)
                    Typeface.ITALIC -> Typeface.defaultFromStyle(Typeface.ITALIC)
                    Typeface.BOLD or Typeface.ITALIC -> Typeface.defaultFromStyle(Typeface.BOLD_ITALIC) // Handle bold | italic flag
                    else -> Typeface.defaultFromStyle(Typeface.NORMAL)
                }

                // Apply the background drawable
                background = this@OtpInputView.boxBackgroundDrawable
                    ?: ContextCompat.getDrawable(
                        context,
                        R.drawable.otp_box_background
                    ) // Fallback to default

                // Add TextWatcher for input changes and focus movement
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (s?.length == 1) {
                            // Move focus to the next EditText
                            if (i < otpDigits - 1) {
                                otpEditTexts[i + 1].requestFocus()
                            } else {
                                // Last digit entered, potentially hide keyboard
                                clearFocus() // Clear focus from the last EditText
                                hideKeyboard()
                                // Optionally trigger a completion listener here
                                // otpCompleteListener?.invoke(getOtp())
                            }
                        } else if (s?.length == 0) {
                            // Backspace in an empty box, move focus to the previous EditText
                            if (i > 0) {
                                otpEditTexts[i - 1].requestFocus()
                                // Optionally clear the previous box if needed, though TextWatcher handles this
                            }
                        }
                    }
                })

                // Handle backspace key press for moving back
                setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                        // If current EditText is empty, move focus to the previous one
                        if (text.isNullOrEmpty()) {
                            if (i > 0) {
                                // Ensure the TextWatcher for the previous box's deletion isn't triggered
                                // This might require more complex handling or relying solely on TextWatcher backspace logic
                                // For now, let's just request focus and TextWatcher will handle the potential deletion
                                otpEditTexts[i - 1].requestFocus()
                            }
                            return@setOnKeyListener true // Consume the event
                        }
                    }
                    false // Let other key events be handled
                }

                // Handle paste action
                customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
                    override fun onCreateActionMode(
                        mode: android.view.ActionMode?,
                        menu: android.view.Menu?
                    ): Boolean = true

                    override fun onPrepareActionMode(
                        mode: android.view.ActionMode?,
                        menu: android.view.Menu?
                    ): Boolean = false

                    override fun onActionItemClicked(
                        mode: android.view.ActionMode?,
                        item: android.view.MenuItem?
                    ): Boolean {
                        when (item?.itemId) {
                            android.R.id.paste -> {
                                pasteOtp()
                                mode?.finish()
                                return true
                            }
                        }
                        return false
                    }

                    override fun onDestroyActionMode(mode: android.view.ActionMode?) {}
                }
                // For newer APIs, use setCustomInsertionActionModeCallback as well if needed
            }
            otpEditTexts.add(editText)
            addView(editText)
        }
    }

    private fun pasteOtp() {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()?.let { pastedText ->
            if (pastedText.isNotEmpty()) {
                for (i in 0 until minOf(pastedText.length, otpDigits)) {
                    otpEditTexts[i].setText(pastedText[i].toString())
                }
                // Move focus to the next available box or the last box if all are filled
                if (pastedText.length < otpDigits) {
                    otpEditTexts[minOf(pastedText.length, otpDigits - 1)].requestFocus()
                } else {
                    if (otpDigits > 0) {
                        otpEditTexts.last().requestFocus()
                    }
                    hideKeyboard()
                }
            }
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var totalWidth = 0
        var maxHeight = 0

        // Measure children first to get their desired size
        children.forEach { child ->
            // Pass UNSPECIFIED height for children initially to let them determine their preferred height
            measureChild(
                child,
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            totalWidth += child.measuredWidth
            maxHeight = maxOf(maxHeight, child.measuredHeight)
        }

        // Add spacing
        totalWidth += (otpDigits - 1) * boxSpacing

        // Add padding
        totalWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom

        val finalWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(totalWidth, widthSize)
            else -> totalWidth // UNSPECIFIED
        }

        val finalHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(maxHeight, heightSize)
            else -> maxHeight // UNSPECIFIED
        }

        setMeasuredDimension(finalWidth, finalHeight)

        // If the width is EXACTLY specified and we need to adjust child widths,
        // remeasure the children with the calculated box width.
        if (widthMode == MeasureSpec.EXACTLY && otpDigits > 0) {
            val availableWidth =
                finalWidth - paddingLeft - paddingRight - (otpDigits - 1) * boxSpacing
            val boxWidth = availableWidth / otpDigits
            val boxWidthSpec = MeasureSpec.makeMeasureSpec(boxWidth, MeasureSpec.EXACTLY)

            children.forEach { child ->
                val childLp = child.layoutParams as LayoutParams
                // Remeasure with the exact calculated width, keeping original height constraints
                val childHeightSpec = getChildMeasureSpec(
                    heightMeasureSpec,
                    paddingTop + paddingBottom,
                    childLp.height
                )
                child.measure(boxWidthSpec, childHeightSpec)
            }
        } else if (heightMode == MeasureSpec.EXACTLY && otpDigits > 0) {
            // If height is EXACTLY, center children vertically within the provided height
            // No need to remeasure children based on height here if their height is WRAP_CONTENT
            // and the parent height is sufficient. Layout handles vertical centering.
        }
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentLeft = paddingLeft
        val parentTop = paddingTop
        val parentBottom = b - t - paddingBottom
        val parentHeight = parentBottom - parentTop

        children.forEach { child ->
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // Center vertically within the available height
            val childTop = parentTop + (parentHeight - childHeight) / 2
            val childBottom = childTop + childHeight
            val childRight = currentLeft + childWidth

            child.layout(currentLeft, childTop, childRight, childBottom)

            currentLeft += childWidth + boxSpacing
        }
    }

    // Public method to get the entered OTP
    fun getOtp(): String {
        return otpEditTexts.joinToString("") { it.text.toString() }
    }

    // Public method to clear the input
    fun clearInput() {
        otpEditTexts.forEach { it.setText("") }
        if (otpDigits > 0) {
            otpEditTexts.first().requestFocus()
        }
    }

    // Method to hide the keyboard
    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }

    // Optional: Add a listener for when the OTP is complete
    private var otpCompleteListener: ((String) -> Unit)? = null

    fun setOtpCompleteListener(listener: (String) -> Unit) {
        otpCompleteListener = listener
    }

    // Trigger the listener when the last digit is entered
    // Modify the afterTextChanged in setupEditTexts:
    /*
       override fun afterTextChanged(s: Editable?) {
           if (s?.length == 1) {
               if (i < otpDigits - 1) {
                   otpEditTexts[i + 1].requestFocus()
               } else {
                   // Last digit entered
                   clearFocus()
                   hideKeyboard()
                   otpCompleteListener?.invoke(getOtp()) // Trigger listener
               }
           } else if (s?.length == 0) {
               if (i > 0) {
                   otpEditTexts[i - 1].requestFocus()
                   // You might want to clear the content of the previous box here
                   // otpEditTexts[i-1].setText("")
               }
           }
       }
     */
}