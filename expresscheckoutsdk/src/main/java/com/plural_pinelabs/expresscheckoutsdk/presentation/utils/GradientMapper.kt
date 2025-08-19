import android.graphics.Color

data class GradientSpec(val colors: List<Int>)

object BankColors {

    private val bankColorMap = mapOf(
        "HDFC" to "#1A2D6A",
        "ICICI" to "#F37E20",
        "AXIS" to "#6A0D2F",
        "KOTAK" to "#EC1B4D",
        "SBI" to "#06A9DE",
        "GENERIC" to "#5B5B5B"
    )

    fun getGradientColors(issuerName: String?): GradientSpec {
        val key = bankColorMap.keys.firstOrNull { issuerName?.contains(it, ignoreCase = true) == true } ?: "GENERIC"
        val baseColor = parseColor(bankColorMap[key]!!)
        val darkColor = darkenColor(baseColor, 0.65f)
        return GradientSpec(listOf(baseColor, darkColor))
    }

    private fun parseColor(hex: String): Int {
        return Color.parseColor(hex)
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceAtLeast(0)
        val g = (Color.green(color) * factor).toInt().coerceAtLeast(0)
        val b = (Color.blue(color) * factor).toInt().coerceAtLeast(0)
        return Color.rgb(r, g, b)
    }
}
