package xyz.travitia.lister.data.model

enum class FontSize {
    SMALL,
    MEDIUM,
    LARGE;

    companion object {
        val DEFAULT = MEDIUM

        fun fromName(name: String?): FontSize {
            return values().find { it.name == name } ?: DEFAULT
        }
    }

    fun getBodyTextSize(): Float = when (this) {
        SMALL -> 14f
        MEDIUM -> 16f
        LARGE -> 20f
    }

    fun getHeaderTextSize(): Float = when (this) {
        SMALL -> 16f
        MEDIUM -> 18f
        LARGE -> 20f
    }
}

