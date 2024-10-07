package vasyl.titles

import androidx.annotation.ColorInt

interface ColorPickerCallback {
    fun onColorChosen(@ColorInt color: Int)
}

