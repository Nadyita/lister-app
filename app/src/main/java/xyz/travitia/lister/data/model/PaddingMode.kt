package xyz.travitia.lister.data.model

enum class PaddingMode {
    NORMAL,
    COMPACT;

    companion object {
        val DEFAULT = NORMAL

        fun fromName(name: String?): PaddingMode {
            return values().find { it.name == name } ?: DEFAULT
        }
    }
}

