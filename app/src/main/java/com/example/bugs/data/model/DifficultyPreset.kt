package com.example.bugs.data.model

enum class DifficultyPreset(
    val title: String,
    val speed: Int,
    val maxBugAmount: Int,
    val bonusInterval: Int,
    val roundDuration: Int
) {
    EASY(
        title = "Лёгкая",
        speed = 3,
        maxBugAmount = 6,
        bonusInterval = 5,
        roundDuration = 60
    ),
    MEDIUM(
        title = "Средняя",
        speed = 5,
        maxBugAmount = 10,
        bonusInterval = 10,
        roundDuration = 60
    ),
    HARD(
        title = "Сложная",
        speed = 8,
        maxBugAmount = 16,
        bonusInterval = 15,
        roundDuration = 90
    ),
    CUSTOM(
        title = "Своя",
        speed = 0,
        maxBugAmount = 0,
        bonusInterval = 0,
        roundDuration = 0
    );

    fun toSettings() = GameSettings(
        speed = speed,
        maxBugAmount = maxBugAmount,
        bonusInterval = bonusInterval,
        roundDuration = roundDuration
    )

    companion object {
        val selectable: List<DifficultyPreset> = listOf(EASY, MEDIUM, HARD)

        val all: List<DifficultyPreset> = entries.toList()

        fun from(settings: GameSettings): DifficultyPreset =
            selectable.firstOrNull { it.matches(settings) } ?: CUSTOM

        private fun DifficultyPreset.matches(s: GameSettings) =
            speed == s.speed &&
            maxBugAmount == s.maxBugAmount &&
            bonusInterval == s.bonusInterval &&
            roundDuration == s.roundDuration

    }
}